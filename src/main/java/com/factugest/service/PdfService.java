package com.factugest.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(78, 115, 223);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(248, 249, 250);
    private static final DeviceRgb DARK_GRAY = new DeviceRgb(73, 80, 87);

    public byte[] generateInvoicePdf(Map<String, Object> invoice, List<Map<String, Object>> details) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(30, 30, 30, 30);

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont regular = PdfFontFactory.createFont("Helvetica");

            // Header table
            Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
            header.setBorder(Border.NO_BORDER);

            // Company info (left)
            Cell companyCell = new Cell().setBorder(Border.NO_BORDER);
            String empresaNombre = str(invoice.get("empresa_nombre"));
            String empresaNit = str(invoice.get("empresa_nit"));
            String empresaDv = str(invoice.get("empresa_dv"));
            String empresaDireccion = str(invoice.get("empresa_direccion"));
            String empresaCiudad = str(invoice.get("empresa_ciudad"));
            String empresaTel = str(invoice.get("empresa_telefono"));
            String empresaCorreo = str(invoice.get("empresa_correo"));

            companyCell.add(new Paragraph(empresaNombre).setFont(bold).setFontSize(14).setFontColor(PRIMARY_COLOR));
            if (!empresaNit.isEmpty()) {
                String nitStr = "NIT: " + empresaNit + (empresaDv.isEmpty() ? "" : "-" + empresaDv);
                companyCell.add(new Paragraph(nitStr).setFont(regular).setFontSize(9).setFontColor(DARK_GRAY));
            }
            if (!empresaDireccion.isEmpty())
                companyCell.add(new Paragraph(empresaDireccion + (empresaCiudad.isEmpty() ? "" : ", " + empresaCiudad)).setFont(regular).setFontSize(9));
            if (!empresaTel.isEmpty())
                companyCell.add(new Paragraph("Tel: " + empresaTel).setFont(regular).setFontSize(9));
            if (!empresaCorreo.isEmpty())
                companyCell.add(new Paragraph(empresaCorreo).setFont(regular).setFontSize(9));
            header.addCell(companyCell);

            // Invoice info (right)
            Cell invoiceCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            String tipoFactura = str(invoice.get("tipo_factura"));
            String tipoLabel = "FV".equals(tipoFactura) ? "FACTURA ELECTRÓNICA" :
                               "NC".equals(tipoFactura) ? "NOTA CRÉDITO" : "NOTA DÉBITO";
            invoiceCell.add(new Paragraph(tipoLabel).setFont(bold).setFontSize(13).setFontColor(PRIMARY_COLOR));
            invoiceCell.add(new Paragraph("N° " + str(invoice.get("cod_factura"))).setFont(bold).setFontSize(11));

            Object fechaObj = invoice.get("fecha");
            String fechaStr = "N/A";
            if (fechaObj instanceof LocalDateTime ldt) {
                fechaStr = ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (fechaObj != null) {
                fechaStr = fechaObj.toString().substring(0, Math.min(10, fechaObj.toString().length()));
            }
            invoiceCell.add(new Paragraph("Fecha: " + fechaStr).setFont(regular).setFontSize(9));
            invoiceCell.add(new Paragraph("Cajero: " + str(invoice.get("usuario_nombre"))).setFont(regular).setFontSize(9));
            header.addCell(invoiceCell);

            doc.add(header);
            doc.add(new Paragraph(" "));

            // Separator
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()).setMarginBottom(10));

            // Client info
            Table clientTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            clientTable.setBorder(Border.NO_BORDER);

            Cell clientCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(LIGHT_GRAY).setPadding(10);
            clientCell.add(new Paragraph("DATOS DEL CLIENTE").setFont(bold).setFontSize(8).setFontColor(PRIMARY_COLOR));
            clientCell.add(new Paragraph(str(invoice.get("cliente_nombre"))).setFont(bold).setFontSize(10));
            clientCell.add(new Paragraph(str(invoice.get("document_type")) + ": " + str(invoice.get("document_number"))).setFont(regular).setFontSize(9));
            clientCell.add(new Paragraph(str(invoice.get("cliente_email"))).setFont(regular).setFontSize(9));
            clientCell.add(new Paragraph(str(invoice.get("cliente_address"))).setFont(regular).setFontSize(9));
            clientTable.addCell(clientCell);

            Cell payCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(LIGHT_GRAY).setPadding(10).setTextAlignment(TextAlignment.RIGHT);
            payCell.add(new Paragraph("INFORMACIÓN DE PAGO").setFont(bold).setFontSize(8).setFontColor(PRIMARY_COLOR));
            payCell.add(new Paragraph("Método: " + str(invoice.get("metodo_pago_nombre"))).setFont(regular).setFontSize(9));
            payCell.add(new Paragraph("Estado: " + str(invoice.get("estado_pago"))).setFont(regular).setFontSize(9));
            clientTable.addCell(payCell);

            doc.add(clientTable);
            doc.add(new Paragraph(" "));

            // Products table
            Table productsTable = new Table(UnitValue.createPercentArray(new float[]{30, 8, 10, 12, 10, 12, 12, 14}))
                .useAllAvailableWidth();

            // Header row
            String[] headers = {"Producto", "SKU", "Cant.", "P. Unit.", "Desc%", "Base Grav.", "IVA", "Total"};
            for (String h : headers) {
                productsTable.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFont(bold).setFontSize(8))
                    .setBackgroundColor(PRIMARY_COLOR)
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.CENTER));
            }

            // Data rows
            boolean alt = false;
            for (Map<String, Object> d : details) {
                DeviceRgb rowColor = alt ? LIGHT_GRAY : new DeviceRgb(255, 255, 255);
                alt = !alt;

                BigDecimal precio = toBD(d.get("precio_unitario"));
                BigDecimal subtotal = toBD(d.get("subtotal"));
                BigDecimal impValor = toBD(d.get("impuesto_valor"));
                BigDecimal totalLinea = subtotal.add(impValor);
                BigDecimal descPct = toBD(d.get("descuento_porcentaje"));

                productsTable.addCell(rowCell(str(d.get("producto_nombre")), regular, 8, rowColor, TextAlignment.LEFT));
                productsTable.addCell(rowCell(str(d.get("sku")), regular, 8, rowColor, TextAlignment.CENTER));
                productsTable.addCell(rowCell(str(d.get("cantidad")), regular, 8, rowColor, TextAlignment.CENTER));
                productsTable.addCell(rowCell(fmt(precio), regular, 8, rowColor, TextAlignment.RIGHT));
                productsTable.addCell(rowCell(descPct.compareTo(BigDecimal.ZERO) > 0 ? descPct.toPlainString() + "%" : "—", regular, 8, rowColor, TextAlignment.RIGHT));
                productsTable.addCell(rowCell(fmt(subtotal), regular, 8, rowColor, TextAlignment.RIGHT));
                productsTable.addCell(rowCell(fmt(impValor), regular, 8, rowColor, TextAlignment.RIGHT));
                productsTable.addCell(rowCell(fmt(totalLinea), bold, 8, rowColor, TextAlignment.RIGHT));
            }

            doc.add(productsTable);
            doc.add(new Paragraph(" "));

            // Totals
            BigDecimal subtotalVal = toBD(invoice.get("subtotal"));
            BigDecimal totalDescuentos = toBD(invoice.get("total_descuentos"));
            BigDecimal totalImpuestos = toBD(invoice.get("total_impuestos"));
            BigDecimal totalVal = toBD(invoice.get("total"));
            BigDecimal subtotalBruto = subtotalVal.add(totalDescuentos);

            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            totalsTable.setBorder(Border.NO_BORDER);
            totalsTable.addCell(new Cell().setBorder(Border.NO_BORDER));

            Table innerTotals = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
            addTotalRow(innerTotals, "Subtotal bruto:", fmt(subtotalBruto), regular, false);
            addTotalRow(innerTotals, "(-) Descuentos:", fmt(totalDescuentos), regular, false);
            addTotalRow(innerTotals, "Base gravable:", fmt(subtotalVal), regular, false);
            addTotalRow(innerTotals, "(+) Impuestos:", fmt(totalImpuestos), regular, false);

            Cell totalLabelCell = new Cell().setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(PRIMARY_COLOR, 1))
                .add(new Paragraph("TOTAL A PAGAR").setFont(bold).setFontSize(10).setFontColor(PRIMARY_COLOR));
            Cell totalValueCell = new Cell().setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(PRIMARY_COLOR, 1))
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(fmt(totalVal)).setFont(bold).setFontSize(12).setFontColor(PRIMARY_COLOR));
            innerTotals.addCell(totalLabelCell);
            innerTotals.addCell(totalValueCell);

            totalsTable.addCell(new Cell().setBorder(Border.NO_BORDER).add(innerTotals));
            doc.add(totalsTable);

            // Observations
            String obs = str(invoice.get("observaciones"));
            if (!obs.isEmpty()) {
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph("Observaciones: " + obs).setFont(regular).setFontSize(9).setFontColor(DARK_GRAY));
            }

            // Footer
            doc.add(new Paragraph(" "));
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()));
            doc.add(new Paragraph("Documento generado por Factugest | " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFont(regular).setFontSize(8).setFontColor(DARK_GRAY).setTextAlignment(TextAlignment.CENTER));

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    private Cell rowCell(String text, PdfFont font, float size, DeviceRgb bg, TextAlignment align) {
        return new Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(size))
            .setBackgroundColor(bg)
            .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(5).setPaddingRight(5)
            .setTextAlignment(align)
            .setBorder(Border.NO_BORDER);
    }

    private void addTotalRow(Table table, String label, String value, PdfFont font, boolean highlight) {
        table.addCell(new Cell().setBorder(Border.NO_BORDER)
            .add(new Paragraph(label).setFont(font).setFontSize(9)));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
            .add(new Paragraph(value).setFont(font).setFontSize(9)));
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private BigDecimal toBD(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "$ 0.00";
        return "$ " + String.format("%,.2f", v);
    }
}
