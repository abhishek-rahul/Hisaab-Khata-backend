package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.Customer;
import com.hisaab_khata.hisaab_khata.domain.CustomerLedger;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerCreateRequest;
//import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerLedgerEntryResponse;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerLedgerResponse;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataPaymentRequest;
import com.hisaab_khata.hisaab_khata.dto.khatadto.PendingKhataResponse;
import com.hisaab_khata.hisaab_khata.enums.LedgerType;
import com.hisaab_khata.hisaab_khata.exception.AccessDeniedException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.mapper.CustomerLedgerMapper;
import com.hisaab_khata.hisaab_khata.mapper.CustomerMapper;
import com.hisaab_khata.hisaab_khata.repository.CustomerLedgerRepository;
import com.hisaab_khata.hisaab_khata.repository.CustomerRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.service.ICustomerService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
//import com.hisaab_khata.hisaab_khata.util.ValidationUtil;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private final CustomerRepository customerRepo;

    @Autowired
    private final CustomerLedgerRepository ledgerRepo;

    @Autowired
    private final ShopRepository shopRepo;

    private final CustomerMapper mapper;
    private final CustomerLedgerMapper ledgerMapper;

    private final ShopContext shopContext;

    @Override
    public CustomerResponse createCustomer(CustomerCreateRequest req) {

        Long shopId = shopContext.getCurrentShopId();

        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shop not found", "SHOP_NOT_FOUND"));

        Customer c = mapper.toEntity(req);
        c.setShop(shop);

        customerRepo.save(c);

        return mapper.toResponse(c);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {

        Long shopId = shopContext.getCurrentShopId();

        return customerRepo.findByShopId(shopId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public CustomerLedgerResponse getCustomerLedger(Long customerId) {

        Long shopId = shopContext.getCurrentShopId();

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found", "CUSTOMER_NOT_FOUND"));

        if (!customer.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Customer not in your shop", "ACCESS_DENIED");
        }

        List<CustomerLedger> entries = ledgerRepo.findByCustomerId(customerId);

        double udhar = entries.stream()
                .filter(e -> e.getType() == LedgerType.UDHAR_DIYA)
                .mapToDouble(CustomerLedger::getAmount)
                .sum();

        double paid = entries.stream()
                .filter(e -> e.getType() == LedgerType.JAMA_HUA)
                .mapToDouble(CustomerLedger::getAmount)
                .sum();

        return CustomerLedgerResponse.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .totalUdhar(udhar)
                .totalPaid(paid)
                .balance(udhar - paid)
                .transactions(
                        entries.stream()
                                .map(ledgerMapper::toEntry)
                                .toList()
                )
                .build();
    }

    @Override
    public void recordKhataPayment(Long customerId, KhataPaymentRequest req) {

        Long shopId = shopContext.getCurrentShopId();

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found", "CUSTOMER_NOT_FOUND"));

        if (!customer.getShop().getId().equals(shopId)) {
            throw new AccessDeniedException("Customer not in your shop", "ACCESS_DENIED");
        }

        CustomerLedger entry = CustomerLedger.builder()
                .customer(customer)
                .type(LedgerType.JAMA_HUA)
                .amount(req.getAmount())
                .build();

        ledgerRepo.save(entry);
    }

    @Override
    public List<PendingKhataResponse> getPendingKhataSummary() {

        Long shopId = shopContext.getCurrentShopId();

        return customerRepo.findByShopId(shopId)
                .stream()
                .map(customer -> {
                    List<CustomerLedger> entries = ledgerRepo.findByCustomerId(customer.getId());

                    double udhar = entries.stream()
                            .filter(e -> e.getType() == LedgerType.UDHAR_DIYA)
                            .mapToDouble(CustomerLedger::getAmount)
                            .sum();

                    double paid = entries.stream()
                            .filter(e -> e.getType() == LedgerType.JAMA_HUA)
                            .mapToDouble(CustomerLedger::getAmount)
                            .sum();

                    double balance = udhar - paid;

                    if (balance <= 0) return null;

                    return new PendingKhataResponse(
                            customer.getId(),
                            customer.getName(),
                            balance
                    );

                })
                .filter(x -> x != null)
                .toList();
    }
}


