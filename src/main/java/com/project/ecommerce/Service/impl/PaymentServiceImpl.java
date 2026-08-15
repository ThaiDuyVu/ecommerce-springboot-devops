package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Config.VNPayConfig;
import com.project.ecommerce.Enums.OrderStatus;
import com.project.ecommerce.Enums.PaymentMethod;
import com.project.ecommerce.Enums.PaymentOrderStatus;
import com.project.ecommerce.Enums.PaymentStatus;
import com.project.ecommerce.Exceptions.InvalidOperationException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Mapper.PaymentOrderMapper;
import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.PaymentOrder;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.OrderRepository;
import com.project.ecommerce.Repository.PaymentOrderRepository;
import com.project.ecommerce.Response.PaymentOrderResponse;
import com.project.ecommerce.Service.PaymentService;
import com.project.ecommerce.Service.TransactionService;
import com.project.ecommerce.Utils.VNPayUtil;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;

    private final OrderRepository orderRepository;

    private final PaymentOrderMapper paymentOrderMapper;

    private final String apiKey = "apikey";

    private final String apiSecret = "apiSecret";

    private final String stripeSecretKey = "stripesecretkey";

    private final VNPayConfig vnPayConfig;

    private final TransactionService transactionService;


    // ============================================================
    // CREATE PAYMENT ORDER
    // ============================================================

    @Override
    @Transactional
    public PaymentOrderResponse createPaymentOrder(
            User user,
            Set<Order> orders,
            PaymentMethod paymentMethod
    ) {

        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Orders cannot be empty"
            );
        }

        long amount =
                orders.stream()
                        .map(Order::getTotalSellingPrice)
                        .filter(Objects::nonNull)
                        .mapToLong(Integer::longValue)
                        .sum();

        PaymentOrder paymentOrder =
                new PaymentOrder();

        paymentOrder.setUser(user);

        paymentOrder.setOrders(orders);

        paymentOrder.setAmount(amount);

        paymentOrder.setPaymentMethod(paymentMethod);

        paymentOrder.setStatus(
                PaymentOrderStatus.PENDING
        );

        /*
         * Transaction reference nội bộ.
         *
         * VNPay sẽ sử dụng transaction reference riêng
         * được tạo từ paymentOrderId trong createVNPayPaymentUrl().
         */
        paymentOrder.setTransactionRef(
                VNPayUtil.generateTxnRef()
        );

        PaymentOrder saved =
                paymentOrderRepository.save(paymentOrder);

        return paymentOrderMapper.toResponse(saved);
    }


    // ============================================================
    // GET PAYMENT ORDER
    // ============================================================

    @Override
    public PaymentOrderResponse getPaymentOrderById(
            Long paymentOrderId
    ) {

        PaymentOrder paymentOrder =
                paymentOrderRepository.findById(paymentOrderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Payment order not found with id: "
                                                + paymentOrderId
                                )
                        );

        return paymentOrderMapper.toResponse(
                paymentOrder
        );
    }


    @Override
    public PaymentOrder getPaymentOrderEntityById(
            Long paymentOrderId
    ) {

        return paymentOrderRepository.findById(paymentOrderId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Payment order not found with id: "
                                        + paymentOrderId
                        )
                );
    }


    @Override
    public PaymentOrder getPaymentOrderEntityByPaymentLinkId(
            String paymentLinkId
    ) {

        return paymentOrderRepository
                .findByPaymentLinkId(paymentLinkId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Payment order not found with payment link id: "
                                        + paymentLinkId
                        )
                );
    }


    // ============================================================
    // RAZORPAY - PROCESS PAYMENT
    // ============================================================

    @Override
    @Transactional
    public PaymentOrderResponse processPaymentOrder(
            Long paymentOrderId,
            String paymentId,
            String paymentLinkId
    ) throws RazorpayException {

        PaymentOrder paymentOrder =
                getPaymentOrderEntityById(
                        paymentOrderId
                );

        if (paymentOrder.getStatus() != PaymentOrderStatus.PENDING) {

            throw new InvalidOperationException(
                    "Payment order has already been processed"
            );
        }

        RazorpayClient razorpayClient =
                new RazorpayClient(
                        apiKey,
                        apiSecret
                );

        Payment payment =
                razorpayClient.payments.fetch(
                        paymentId
                );

        String razorpayStatus =
                payment.get("status");

        if ("captured".equalsIgnoreCase(razorpayStatus)) {

            paymentOrder.setStatus(
                    PaymentOrderStatus.SUCCESS
            );

            paymentOrder.setPaymentLinkId(
                    paymentLinkId
            );

            PaymentOrder savedPaymentOrder =
                    paymentOrderRepository.save(
                            paymentOrder
                    );

            for (Order order : savedPaymentOrder.getOrders()) {

                order.getPaymentDetails()
                        .setPaymentId(
                                paymentId
                        );

                order.getPaymentDetails()
                        .setRazorpayPaymentLinkId(
                                paymentLinkId
                        );

                order.getPaymentDetails()
                        .setPaymentStatus(
                                PaymentStatus.COMPLETED
                        );

                Order savedOrder =
                        orderRepository.save(
                                order
                        );

                transactionService.createTransaction(
                        savedPaymentOrder,
                        savedOrder
                );
            }

            return paymentOrderMapper.toResponse(
                    savedPaymentOrder
            );
        }

        paymentOrder.setStatus(
                PaymentOrderStatus.FAILED
        );

        paymentOrder.setPaymentLinkId(
                paymentLinkId
        );

        PaymentOrder savedPaymentOrder =
                paymentOrderRepository.save(
                        paymentOrder
                );

        for (Order order : savedPaymentOrder.getOrders()) {

            order.getPaymentDetails()
                    .setPaymentId(
                            paymentId
                    );

            order.getPaymentDetails()
                    .setRazorpayPaymentLinkId(
                            paymentLinkId
                    );

            order.getPaymentDetails()
                    .setPaymentStatus(
                            PaymentStatus.FAILED
                    );

            orderRepository.save(
                    order
            );
        }

        return paymentOrderMapper.toResponse(
                savedPaymentOrder
        );
    }


    // ============================================================
    // RAZORPAY - CREATE PAYMENT LINK
    // ============================================================

    @Override
    @Transactional
    public String createRazorpayPaymentLink(
            User user,
            Long amount,
            Long paymentOrderId
    ) throws RazorpayException {

        if (user == null) {

            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (amount == null || amount <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        PaymentOrder paymentOrder =
                getPaymentOrderEntityById(
                        paymentOrderId
                );

        RazorpayClient razorpayClient =
                new RazorpayClient(
                        apiKey,
                        apiSecret
                );

        JSONObject request =
                new JSONObject();

        request.put(
                "amount",
                amount * 100
        );

        request.put(
                "currency",
                "INR"
        );

        JSONObject customer =
                new JSONObject();

        customer.put(
                "name",
                user.getFullName()
        );

        customer.put(
                "email",
                user.getEmail()
        );

        request.put(
                "customer",
                customer
        );

        JSONObject notify =
                new JSONObject();

        notify.put(
                "email",
                true
        );

        request.put(
                "notify",
                notify
        );

        request.put(
                "callback_url",
                "http://localhost:3000/payment-success/"
                        + paymentOrderId
        );

        request.put(
                "callback_method",
                "GET"
        );

        request.put(
                "reference_id",
                paymentOrderId.toString()
        );

        request.put(
                "description",
                "Payment Order #" + paymentOrderId
        );

        PaymentLink paymentLink =
                razorpayClient.paymentLink.create(
                        request
                );

        paymentOrder.setPaymentLinkId(
                paymentLink.get("id")
        );

        paymentOrderRepository.save(
                paymentOrder
        );

        return paymentLink.get("short_url");
    }


    // ============================================================
    // STRIPE - CREATE PAYMENT LINK
    // ============================================================

    @Override
    public String createStripePaymentLink(
            User user,
            Long amount,
            Long paymentOrderId
    ) throws StripeException {

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params =
                SessionCreateParams.builder()

                        .addPaymentMethodType(
                                SessionCreateParams.PaymentMethodType.CARD
                        )

                        .setMode(
                                SessionCreateParams.Mode.PAYMENT
                        )

                        .setSuccessUrl(
                                "http://localhost:3000/payment-success/"
                                        + paymentOrderId
                        )

                        .setCancelUrl(
                                "http://localhost:3000/payment-cancel"
                        )

                        .addLineItem(

                                SessionCreateParams.LineItem.builder()

                                        .setQuantity(1L)

                                        .setPriceData(

                                                SessionCreateParams
                                                        .LineItem
                                                        .PriceData
                                                        .builder()

                                                        .setCurrency("usd")

                                                        .setUnitAmount(
                                                                amount * 100
                                                        )

                                                        .setProductData(

                                                                SessionCreateParams
                                                                        .LineItem
                                                                        .PriceData
                                                                        .ProductData
                                                                        .builder()

                                                                        .setName(
                                                                                "Order #" + paymentOrderId
                                                                        )

                                                                        .build()
                                                        )

                                                        .build()

                                        )

                                        .build()

                        )

                        .build();

        Session session =
                Session.create(params);

        return session.getUrl();
    }


    // ============================================================
    // VNPAY - CREATE PAYMENT URL
    // ============================================================

    @Override
    public String createVNPayPaymentUrl(
            User user,
            Long amount,
            Long paymentOrderId
    ) {

        if (user == null) {

            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (amount == null || amount <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        /*
         * VNPay TxnRef phải chứa paymentOrderId để callback
         * có thể xác định PaymentOrder tương ứng.
         *
         * Ví dụ:
         *
         * 15-AB12CD34EF56
         */
        String transactionRef =
                VNPayUtil.generateTxnRef(paymentOrderId);

        Map<String, String> params =
                buildVNPayParams(
                        user,
                        amount,
                        transactionRef,
                        paymentOrderId
                );

        String hashData =
                VNPayUtil.buildHashData(
                        new TreeMap<>(params)
                );

        String secureHash =
                VNPayUtil.hmacSHA512(
                        vnPayConfig.getHashSecret(),
                        hashData
                );

        String query =
                VNPayUtil.buildQuery(
                        new TreeMap<>(params)
                );

        return vnPayConfig.getPayUrl()
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;
    }


    // ============================================================
    // VNPAY - CALLBACK
    // ============================================================

    @Override
    @Transactional
    public PaymentOrderResponse processVNPayPayment(
            Map<String, String> params
    ) {

        String txnRef =
                params.get("vnp_TxnRef");

        String responseCode =
                params.get("vnp_ResponseCode");

        String transactionStatus =
                params.get("vnp_TransactionStatus");

        String transactionNo =
                params.get("vnp_TransactionNo");

        String secureHash =
                params.get("vnp_SecureHash");

        if (txnRef == null || txnRef.isBlank()) {

            throw new InvalidOperationException(
                    "VNPay transaction reference is missing"
            );
        }

        /*
         * TxnRef được tạo theo format:
         *
         * paymentOrderId-random
         *
         * Ví dụ:
         *
         * 15-AB12CD34EF56
         */
        String[] txnRefParts =
                txnRef.split("-");

        if (txnRefParts.length < 2) {

            throw new InvalidOperationException(
                    "Invalid VNPay transaction reference"
            );
        }

        Long paymentOrderId;

        try {

            paymentOrderId =
                    Long.parseLong(
                            txnRefParts[0]
                    );

        } catch (NumberFormatException e) {

            throw new InvalidOperationException(
                    "Invalid payment order id in VNPay transaction reference"
            );
        }

        PaymentOrder paymentOrder =
                getPaymentOrderEntityById(
                        paymentOrderId
                );

        /*
         * Không cho phép xử lý lại PaymentOrder
         * đã thành công hoặc thất bại.
         */
        if (paymentOrder.getStatus()
                != PaymentOrderStatus.PENDING) {

            throw new InvalidOperationException(
                    "Payment order has already been processed"
            );
        }

        /*
         * Verify VNPay signature trước khi cập nhật database.
         */
        TreeMap<String, String> hashParams =
                new TreeMap<>(params);

        hashParams.remove("vnp_SecureHash");

        hashParams.remove("vnp_SecureHashType");

        String hashData =
                VNPayUtil.buildHashData(
                        hashParams
                );

        String calculatedHash =
                VNPayUtil.hmacSHA512(
                        vnPayConfig.getHashSecret(),
                        hashData
                );

        if (secureHash == null
                || !calculatedHash.equalsIgnoreCase(
                secureHash
        )) {

            throw new InvalidOperationException(
                    "Invalid VNPay signature"
            );
        }

        boolean success =
                "00".equals(responseCode)
                        &&
                        "00".equals(transactionStatus);

        if (success) {

            paymentOrder.setStatus(
                    PaymentOrderStatus.SUCCESS
            );

            paymentOrder.setPaymentLinkId(
                    transactionNo
            );

            PaymentOrder savedPaymentOrder =
                    paymentOrderRepository.save(
                            paymentOrder
                    );

            for (Order order :
                    savedPaymentOrder.getOrders()) {

                order.getPaymentDetails()
                        .setPaymentId(
                                transactionNo
                        );

                order.getPaymentDetails()
                        .setPaymentStatus(
                                PaymentStatus.COMPLETED
                        );

                order.setOrderStatus(
                        OrderStatus.CONFIRMED
                );

                Order savedOrder =
                        orderRepository.save(
                                order
                        );

                transactionService.createTransaction(
                        savedPaymentOrder,
                        savedOrder
                );
            }

            return paymentOrderMapper.toResponse(
                    savedPaymentOrder
            );
        }

        /*
         * VNPay trả về giao dịch thất bại.
         */
        paymentOrder.setStatus(
                PaymentOrderStatus.FAILED
        );

        PaymentOrder savedPaymentOrder =
                paymentOrderRepository.save(
                        paymentOrder
                );

        for (Order order :
                savedPaymentOrder.getOrders()) {

            order.getPaymentDetails()
                    .setPaymentId(
                            transactionNo
                    );

            order.getPaymentDetails()
                    .setPaymentStatus(
                            PaymentStatus.FAILED
                    );

            orderRepository.save(
                    order
            );
        }

        return paymentOrderMapper.toResponse(
                savedPaymentOrder
        );
    }


    // ============================================================
    // VNPAY - OLD CALLBACK METHOD
    // ============================================================

    /*
     * Giữ method này để không phá vỡ PaymentService interface
     * hoặc code Controller hiện tại nếu đang sử dụng nó.
     *
     * Tuy nhiên flow chính nên sử dụng processVNPayPayment().
     */
    @Override
    @Transactional
    public PaymentOrderResponse processVNPayCallback(
            Map<String, String> params
    ) {

        boolean valid =
                VNPayUtil.verifySignature(
                        params,
                        vnPayConfig.getHashSecret()
                );

        if (!valid) {

            throw new InvalidOperationException(
                    "Invalid VNPay signature"
            );
        }

        String responseCode =
                params.get("vnp_ResponseCode");

        String transactionStatus =
                params.get("vnp_TransactionStatus");

        String txnRef =
                params.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {

            throw new InvalidOperationException(
                    "VNPay transaction reference is missing"
            );
        }

        String[] txnRefParts =
                txnRef.split("-");

        if (txnRefParts.length < 2) {

            throw new InvalidOperationException(
                    "Invalid VNPay transaction reference"
            );
        }

        Long paymentOrderId;

        try {

            paymentOrderId =
                    Long.parseLong(
                            txnRefParts[0]
                    );

        } catch (NumberFormatException e) {

            throw new InvalidOperationException(
                    "Invalid payment order id in VNPay transaction reference"
            );
        }

        PaymentOrder paymentOrder =
                paymentOrderRepository.findById(
                                paymentOrderId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Payment order not found with id: "
                                                + paymentOrderId
                                )
                        );

        if (paymentOrder.getStatus()
                != PaymentOrderStatus.PENDING) {

            throw new InvalidOperationException(
                    "Payment order has already been processed"
            );
        }

        boolean success =
                "00".equals(responseCode)
                        &&
                        "00".equals(transactionStatus);

        String transactionNo =
                params.get("vnp_TransactionNo");

        if (success) {

            paymentOrder.setStatus(
                    PaymentOrderStatus.SUCCESS
            );

            paymentOrder.setPaymentLinkId(
                    transactionNo
            );

            for (Order order :
                    paymentOrder.getOrders()) {

                order.getPaymentDetails()
                        .setPaymentId(
                                transactionNo
                        );

                order.getPaymentDetails()
                        .setPaymentStatus(
                                PaymentStatus.COMPLETED
                        );

                order.setOrderStatus(
                        OrderStatus.CONFIRMED
                );

                orderRepository.save(
                        order
                );
            }

        } else {

            paymentOrder.setStatus(
                    PaymentOrderStatus.FAILED
            );

            for (Order order :
                    paymentOrder.getOrders()) {

                order.getPaymentDetails()
                        .setPaymentId(
                                transactionNo
                        );

                order.getPaymentDetails()
                        .setPaymentStatus(
                                PaymentStatus.FAILED
                        );

                orderRepository.save(
                        order
                );
            }
        }

        PaymentOrder saved =
                paymentOrderRepository.save(
                        paymentOrder
                );

        return paymentOrderMapper.toResponse(
                saved
        );
    }


    // ============================================================
    // VNPAY - BUILD PARAMETERS
    // ============================================================

    private Map<String, String> buildVNPayParams(
            User user,
            Long amount,
            String transactionRef,
            Long paymentOrderId
    ) {

        Map<String, String> params =
                new HashMap<>();

        params.put(
                "vnp_Version",
                vnPayConfig.getVersion()
        );

        params.put(
                "vnp_Command",
                vnPayConfig.getCommand()
        );

        params.put(
                "vnp_TmnCode",
                vnPayConfig.getTmnCode()
        );

        params.put(
                "vnp_Amount",
                String.valueOf(amount * 100)
        );

        params.put(
                "vnp_CurrCode",
                vnPayConfig.getCurrency()
        );

        params.put(
                "vnp_TxnRef",
                transactionRef
        );

        params.put(
                "vnp_OrderInfo",
                "Payment order #" + paymentOrderId
        );

        params.put(
                "vnp_OrderType",
                vnPayConfig.getOrderType()
        );

        params.put(
                "vnp_Locale",
                vnPayConfig.getLocale()
        );

        params.put(
                "vnp_ReturnUrl",
                vnPayConfig.getReturnUrl()
        );

        params.put(
                "vnp_CreateDate",
                VNPayUtil.getCurrentDate()
        );

        params.put(
                "vnp_ExpireDate",
                VNPayUtil.getExpireDate()
        );

        params.put(
                "vnp_IpAddr",
                "127.0.0.1"
        );

        params.put(
                "vnp_Bill_FirstName",
                user.getFullName()
        );

        params.put(
                "vnp_Bill_Email",
                user.getEmail()
        );

        return params;
    }
}