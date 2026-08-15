package com.project.ecommerce.Utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class VNPayUtil {

    private VNPayUtil() {
    }

    /**
     * yyyyMMddHHmmss
     */
    public static String getCurrentDate() {

        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmss"
                        )
                );
    }

    /**
     * Mặc định hết hạn sau 15 phút
     */
    public static String getExpireDate() {

        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .plusMinutes(15)
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmss"
                        )
                );
    }

    /**
     * Mã giao dịch nội bộ
     */
    public static String generateTxnRef() {

        return "PAY-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();

    }

    /**
     * UTF-8 encode
     */
    public static String urlEncode(
            String value
    ) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );

    }

    /**
     * Sinh chữ ký SHA512
     */
    public static String hmacSHA512(
            String key,
            String data
    ) {

        try {

            Mac mac =
                    Mac.getInstance(
                            "HmacSHA512"
                    );

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            key.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA512"
                    );

            mac.init(secretKey);

            byte[] hashBytes =
                    mac.doFinal(
                            data.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hash =
                    new StringBuilder();

            for (byte b : hashBytes) {

                hash.append(
                        String.format(
                                "%02x",
                                b
                        )
                );

            }

            return hash.toString();

        }
        catch (Exception e) {

            throw new RuntimeException(
                    "Cannot generate HMAC SHA512",
                    e
            );

        }

    }

    /**
     * Build query để redirect VNPay
     *
     * value sẽ được URL Encode
     */
    public static String buildQuery(
            Map<String, String> params
    ) {

        StringBuilder query =
                new StringBuilder();

        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {

            if (entry.getValue() == null
                    || entry.getValue().isBlank()) {

                continue;

            }

            if (!first) {

                query.append("&");

            }

            query.append(entry.getKey())
                    .append("=")
                    .append(
                            urlEncode(
                                    entry.getValue()
                            )
                    );

            first = false;

        }

        return query.toString();

    }

    /**
     * Build raw hash data
     *
     * Không encode
     */
    public static String buildHashData(
            Map<String, String> params
    ) {

        StringBuilder hashData =
                new StringBuilder();

        boolean first = true;


        for (Map.Entry<String, String> entry : params.entrySet()) {


            if (entry.getValue() == null
                    || entry.getValue().isBlank()) {

                continue;

            }


            if (!first) {

                hashData.append("&");

            }


            hashData.append(
                    entry.getKey()
            );


            hashData.append("=");


            hashData.append(
                    URLEncoder.encode(
                            entry.getValue(),
                            StandardCharsets.US_ASCII
                    )
            );
            first = false;

        }
        return hashData.toString();

    }

    /**
     * TreeMap giúp sort key theo alphabet
     */
    public static TreeMap<String, String> newSortedMap() {

        return new TreeMap<>();

    }
    public static boolean verifySignature(
            Map<String,String> params,
            String hashSecret
    ){

        String secureHash =
                params.get("vnp_SecureHash");


        Map<String,String> hashParams =
                new TreeMap<>(params);


        hashParams.remove("vnp_SecureHash");

        hashParams.remove("vnp_SecureHashType");


        String hashData =
                buildHashData(hashParams);


        String calculatedHash =
                hmacSHA512(
                        hashSecret,
                        hashData
                );


        return calculatedHash.equals(
                secureHash
        );

    }

    public static String generateTxnRef(Long paymentOrderId) {

        if (paymentOrderId == null) {
            throw new IllegalArgumentException(
                    "Payment order id cannot be null"
            );
        }

        return paymentOrderId +
                "-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();
    }

}