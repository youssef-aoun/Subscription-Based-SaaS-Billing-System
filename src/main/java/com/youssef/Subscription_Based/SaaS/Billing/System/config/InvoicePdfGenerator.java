package com.youssef.Subscription_Based.SaaS.Billing.System.config;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.FontFactory;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments.Invoice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class InvoicePdfGenerator {

    public static ByteArrayInputStream generateInvoicePdf(Invoice invoice){

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try{
            PdfWriter.getInstance(document, out);
            document.open();

            //Header
            document.add(new Paragraph("SaaS Billing Inc.", FontFactory.getFont(FontFactory.HELVETICA)));
            document.add(new Paragraph("Invoice #" + invoice.getId()));
            document.add(new Paragraph("Issued Date: " + invoice.getIssuedDate()));
            document.add(new Paragraph("Due Date: " + invoice.getDueDate()));
            document.add(new Paragraph(" "));

            // Customer Info
            document.add(new Paragraph("Customer Email: " + invoice.getUser().getEmail()));
            document.add(new Paragraph(" "));

            // Payment Info
            document.add(new Paragraph("Subscription Plan: " + invoice.getSubscription().getPlan().getName()));
            document.add(new Paragraph("Amount: " + invoice.getAmount() + " USD"));
            document.add(new Paragraph("Status: " + invoice.getStatus()));
            document.add(new Paragraph("Stripe Invoice ID: " + invoice.getStripeInvoiceId()));
            document.add(new Paragraph(" "));

            // Footer
            document.add(new Paragraph("Thank you for your business!"));

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error while creating PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}