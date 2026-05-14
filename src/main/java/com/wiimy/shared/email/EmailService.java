package com.wiimy.shared.email;

import com.wiimy.order.entity.Order;
import com.wiimy.order.entity.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.admin}")
    private String adminAddress;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Sends an order-confirmation email to the customer and a notification to
     * the store admin. Runs in a separate thread so it never blocks the HTTP response.
     */
    @Async
    public void sendOrderConfirmation(Order order) {
        sendToCustomer(order);
        sendToAdmin(order);
    }

    @Async
    public void sendDispatchConfirmation(Order order) {
        String subject = "WIIMY — Tu pedido está en camino: " + order.getOrderNumber();
        send(order.getCustomerEmail(), subject, buildDispatchHtml(order));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void sendToCustomer(Order order) {
        String subject = "WIIMY — Orden recibida: " + order.getOrderNumber();
        String body = buildCustomerHtml(order);
        send(order.getCustomerEmail(), subject, body);
    }

    private void sendToAdmin(Order order) {
        String subject = "[WIIMY] Nueva orden: " + order.getOrderNumber()
                + " — $" + fmt(order.getTotal());
        String body = buildAdminHtml(order);
        send(adminAddress, subject, body);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email enviado a {} — {}", to, subject);
        } catch (MessagingException ex) {
            log.error("Error al enviar email a {}: {}", to, ex.getMessage());
        }
    }

    // ── HTML templates ────────────────────────────────────────────────────────

    private String buildCustomerHtml(Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            items.append("""
                    <tr>
                      <td style="padding:12px 0;border-bottom:1px solid #2a2a2a;">
                        <strong style="color:#ffffff;font-size:14px;">%s</strong><br>
                        <span style="color:#888;font-size:12px;">Cantidad: %d</span>
                      </td>
                    </tr>
                    """.formatted(
                    escHtml(item.getProductName()),
                    item.getQuantity()
            ));
        }

        return baseLayout("""
                <h2 style="font-family:'Arial Black',Arial,sans-serif;font-size:28px;
                           letter-spacing:4px;color:#ffffff;margin:0 0 8px 0;">
                  ORDEN CONFIRMADA
                </h2>
                <p style="color:#ff5c35;font-size:11px;letter-spacing:3px;
                          text-transform:uppercase;margin:0 0 32px 0;">
                  %s
                </p>

                <p style="color:#aaa;font-size:14px;line-height:1.6;margin:0 0 32px 0;">
                  Hola <strong style="color:#fff;">%s</strong>,<br>
                  recibimos tu pedido. Recibirás un mensaje en nuestro canal de
                  <strong style="color:#fff;">WhatsApp</strong>. Allí te proporcionaremos
                  nuestras opciones de transferencia y acordaremos el valor del envío,
                  el cual depende de tu ciudad, barrio y dimensiones del paquete.
                </p>

                <!-- Items -->
                <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                  %s
                </table>

                <!-- Shipping -->
                <div style="background:#1a1a1a;border:1px solid #2a2a2a;
                            border-radius:4px;padding:20px;margin-bottom:32px;">
                  <p style="color:#ff5c35;font-size:10px;letter-spacing:3px;
                             text-transform:uppercase;margin:0 0 10px 0;">
                    Dirección de entrega
                  </p>
                  <p style="color:#ccc;font-size:13px;line-height:1.7;margin:0;">
                    %s<br>%s%s%s
                  </p>
                </div>

                <p style="color:#555;font-size:12px;text-align:center;margin:0;">
                  Si tenés alguna duda respondé este email o escribinos a
                  <a href="mailto:hola@wiimy.store" style="color:#ff5c35;">hola@wiimy.store</a>
                </p>
                """.formatted(
                escHtml(order.getOrderNumber()),
                escHtml(order.getCustomerName().split(" ")[0]),
                items,
                escHtml(order.getShippingAddress().getStreet()),
                escHtml(order.getShippingAddress().getCity()),
                order.getShippingAddress().getState() != null
                        ? ", " + escHtml(order.getShippingAddress().getState()) : "",
                order.getShippingAddress().getZipCode() != null
                        ? " (" + escHtml(order.getShippingAddress().getZipCode()) + ")" : ""
        ));
    }

    private String buildDispatchHtml(Order order) {
        StringBuilder rows = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            BigDecimal subtotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            rows.append("""
                    <tr>
                      <td style="padding:10px 0;border-bottom:1px solid #2a2a2a;
                                 color:#ffffff;font-size:14px;">
                        %s<br>
                        <span style="color:#888;font-size:12px;">%d × $%s</span>
                      </td>
                      <td style="padding:10px 0;border-bottom:1px solid #2a2a2a;
                                 text-align:right;color:#ffffff;font-size:14px;font-weight:bold;">
                        $%s
                      </td>
                    </tr>
                    """.formatted(
                    escHtml(item.getProductName()),
                    item.getQuantity(),
                    fmt(item.getUnitPrice()),
                    fmt(subtotal)
            ));
        }

        String shippingRow = order.getShippingCost().compareTo(BigDecimal.ZERO) > 0
                ? """
                <tr>
                  <td style="padding:10px 0;color:#888;font-size:13px;">Costo de envío</td>
                  <td style="padding:10px 0;text-align:right;color:#888;font-size:13px;">$%s</td>
                </tr>
                """.formatted(fmt(order.getShippingCost()))
                : "";

        return baseLayout("""
                <h2 style="font-family:'Arial Black',Arial,sans-serif;font-size:28px;
                           letter-spacing:4px;color:#ffffff;margin:0 0 8px 0;">
                  PEDIDO DESPACHADO
                </h2>
                <p style="color:#ff5c35;font-size:11px;letter-spacing:3px;
                          text-transform:uppercase;margin:0 0 32px 0;">
                  %s
                </p>

                <p style="color:#aaa;font-size:14px;line-height:1.6;margin:0 0 32px 0;">
                  Hola <strong style="color:#fff;">%s</strong>,<br>
                  tu pedido ha sido despachado. A continuación encontrás el detalle de tu factura.
                </p>

                <!-- Invoice -->
                <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                  %s
                  %s
                  <tr>
                    <td style="padding:14px 0 0 0;
                               color:#888;font-size:13px;border-top:1px solid #2a2a2a;">
                      Subtotal productos
                    </td>
                    <td style="padding:14px 0 0 0;text-align:right;
                               color:#888;font-size:13px;border-top:1px solid #2a2a2a;">
                      $%s
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:8px 0;font-weight:bold;
                               font-size:15px;color:#ffffff;">
                      TOTAL
                    </td>
                    <td style="padding:8px 0;text-align:right;">
                      <span style="font-family:'Arial Black',Arial,sans-serif;
                                   font-size:26px;color:#ff5c35;">
                        $%s
                      </span>
                    </td>
                  </tr>
                </table>

                <!-- Shipping address -->
                <div style="background:#1a1a1a;border:1px solid #2a2a2a;
                            border-radius:4px;padding:20px;margin-bottom:32px;">
                  <p style="color:#ff5c35;font-size:10px;letter-spacing:3px;
                             text-transform:uppercase;margin:0 0 10px 0;">
                    Dirección de entrega
                  </p>
                  <p style="color:#ccc;font-size:13px;line-height:1.7;margin:0;">
                    %s<br>%s%s%s
                  </p>
                </div>

                <p style="color:#555;font-size:12px;text-align:center;margin:0;">
                  Si tenés alguna duda escribinos a
                  <a href="mailto:hola@wiimy.store" style="color:#ff5c35;">hola@wiimy.store</a>
                </p>
                """.formatted(
                escHtml(order.getOrderNumber()),
                escHtml(order.getCustomerName().split(" ")[0]),
                rows,
                shippingRow,
                fmt(order.getSubtotal()),
                fmt(order.getTotal()),
                escHtml(order.getShippingAddress().getStreet()),
                escHtml(order.getShippingAddress().getCity()),
                order.getShippingAddress().getState() != null
                        ? ", " + escHtml(order.getShippingAddress().getState()) : "",
                order.getShippingAddress().getZipCode() != null
                        ? " (" + escHtml(order.getShippingAddress().getZipCode()) + ")" : ""
        ));
    }

    private String buildAdminHtml(Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            items.append("<li style='color:#ccc;margin-bottom:4px;'>")
                    .append(item.getQuantity()).append("× ")
                    .append(escHtml(item.getProductName()))
                    .append(" — $").append(fmt(item.getUnitPrice()))
                    .append("</li>");
        }

        return baseLayout("""
                <h2 style="font-family:'Arial Black',Arial,sans-serif;font-size:24px;
                           letter-spacing:3px;color:#ffffff;margin:0 0 8px 0;">
                  NUEVA ORDEN
                </h2>
                <p style="color:#ff5c35;font-size:11px;letter-spacing:3px;
                          text-transform:uppercase;margin:0 0 28px 0;">
                  %s
                </p>

                <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                  <tr>
                    <td style="color:#888;font-size:13px;padding:6px 0;">Cliente</td>
                    <td style="color:#fff;font-size:13px;padding:6px 0;text-align:right;">
                      %s &lt;%s&gt;
                    </td>
                  </tr>
                  <tr>
                    <td style="color:#888;font-size:13px;padding:6px 0;">Teléfono</td>
                    <td style="color:#fff;font-size:13px;padding:6px 0;text-align:right;">
                      %s
                    </td>
                  </tr>
                  <tr>
                    <td style="color:#888;font-size:13px;padding:6px 0;">Total</td>
                    <td style="color:#ff5c35;font-weight:bold;font-size:16px;
                               padding:6px 0;text-align:right;">
                      $%s
                    </td>
                  </tr>
                </table>

                <p style="color:#ff5c35;font-size:10px;letter-spacing:3px;
                           text-transform:uppercase;margin:0 0 8px 0;">
                  Productos
                </p>
                <ul style="padding-left:16px;margin:0 0 24px 0;">%s</ul>

                <p style="color:#ff5c35;font-size:10px;letter-spacing:3px;
                           text-transform:uppercase;margin:0 0 8px 0;">
                  Envío
                </p>
                <p style="color:#ccc;font-size:13px;line-height:1.7;margin:0 0 24px 0;">
                  %s, %s%s%s
                </p>
                """.formatted(
                escHtml(order.getOrderNumber()),
                escHtml(order.getCustomerName()),
                escHtml(order.getCustomerEmail()),
                order.getCustomerPhone() != null ? escHtml(order.getCustomerPhone()) : "—",
                fmt(order.getTotal()),
                items,
                escHtml(order.getShippingAddress().getStreet()),
                escHtml(order.getShippingAddress().getCity()),
                order.getShippingAddress().getState() != null
                        ? ", " + escHtml(order.getShippingAddress().getState()) : "",
                order.getShippingAddress().getZipCode() != null
                        ? " (" + escHtml(order.getShippingAddress().getZipCode()) + ")" : ""
        ));
    }

    /** Wraps content in the shared WIIMY email chrome. */
    private static String baseLayout(String content) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <title>WIIMY</title>
                </head>
                <body style="margin:0;padding:0;background:#0d0d0d;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#0d0d0d;padding:40px 20px;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0"
                               style="max-width:560px;width:100%%;">

                          <!-- Logo -->
                          <tr>
                            <td style="padding-bottom:32px;border-bottom:1px solid #2a2a2a;
                                       text-align:center;">
                              <span style="font-family:'Arial Black',Arial,sans-serif;
                                           font-size:28px;letter-spacing:6px;color:#ffffff;">
                                WIIMY<span style="color:#ff5c35;">.</span>STORE
                              </span>
                            </td>
                          </tr>

                          <!-- Body -->
                          <tr>
                            <td style="padding:36px 0;">
                              %s
                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="border-top:1px solid #2a2a2a;padding-top:24px;
                                       text-align:center;">
                              <p style="color:#444;font-size:11px;letter-spacing:2px;
                                        text-transform:uppercase;margin:0;">
                                WIIMY — Sublimación premium para la cultura geek
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(content);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String fmt(BigDecimal value) {
        return NumberFormat.getNumberInstance(new Locale("es", "AR"))
                .format(value);
    }

    private static String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
