package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;

import java.io.ByteArrayInputStream;

public interface InvoiceService {

    ByteArrayInputStream downloadInvoicePdf(Long invoiceId, User currentUser);
}
