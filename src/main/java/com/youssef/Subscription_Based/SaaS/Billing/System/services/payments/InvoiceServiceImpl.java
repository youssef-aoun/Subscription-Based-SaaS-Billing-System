package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

import com.youssef.Subscription_Based.SaaS.Billing.System.config.InvoicePdfGenerator;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.InvoiceRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments.Invoice;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public ByteArrayInputStream downloadInvoicePdf(Long invoiceId, User currentUser) {
        Invoice invoice = invoiceRepository.findInvoiceById(invoiceId).orElseThrow(() -> new IllegalStateException("Invoice not found"));

        boolean isOwner = invoice.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to access this invoice.");
        }

        return InvoicePdfGenerator.generateInvoicePdf(invoice);
    }
}
