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

import java.util.*;


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

        PaymentOrder saved =
                paymentOrderRepository.save(paymentOrder);

        return paymentOrderMapper.toResponse(saved);

    }

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

    @Override
    public String createVNPayPaymentUrl(
            User user,
            Long amount,
            Long paymentOrderId
    ) {

        if(user == null){

            throw new IllegalArgumentException(
                    "User cannot be null"
            );

        }


        if(amount == null || amount <= 0){

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );

        }

        Map<String,String> params =
                buildVNPayParams(
                        user,
                        amount,
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

    @Override
    @Transactional
    public PaymentOrderResponse processVNPayCallback(
            Map<String,String> params
    ){

        boolean valid =
                VNPayUtil.verifySignature(
                        params,
                        vnPayConfig.getHashSecret()
                );


        if(!valid){

            throw new RuntimeException(
                    "Invalid VNPay signature"
            );

        }


        String responseCode =
                params.get("vnp_ResponseCode");


        String transactionStatus =
                params.get("vnp_TransactionStatus");


        Long paymentOrderId =
                Long.parseLong(
                        params.get("vnp_TxnRef")
                );


        PaymentOrder paymentOrder =
                paymentOrderRepository.findById(
                                paymentOrderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Payment order not found"
                                )
                        );


        if(
                "00".equals(responseCode)
                        &&
                        "00".equals(transactionStatus)
        ){

            paymentOrder.setStatus(
                    PaymentOrderStatus.SUCCESS
            );


            for(Order order :
                    paymentOrder.getOrders()){


                order.getPaymentDetails()
                        .setPaymentStatus(
                                PaymentStatus.COMPLETED
                        );


                order.setOrderStatus(
                        OrderStatus.CONFIRMED
                );


                orderRepository.save(order);

            }


        }
        else {


            paymentOrder.setStatus(
                    PaymentOrderStatus.FAILED
            );


            for(Order order :
                    paymentOrder.getOrders()){

                order.getPaymentDetails()
                        .setPaymentStatus(
                                PaymentStatus.FAILED
                        );

                orderRepository.save(order);

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

        Long paymentOrderId =
                Long.parseLong(
                        txnRef.split("-")[0]
                );

        PaymentOrder paymentOrder =
                getPaymentOrderEntityById(
                        paymentOrderId
                );

        if (paymentOrder.getStatus() != PaymentOrderStatus.PENDING) {

            throw new InvalidOperationException(
                    "Payment order has already been processed"
            );

        }

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

        if (!calculatedHash.equalsIgnoreCase(secureHash)) {

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

            for (Order order : savedPaymentOrder.getOrders()) {

                order.getPaymentDetails()
                        .setPaymentId(
                                transactionNo
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

        PaymentOrder savedPaymentOrder =
                paymentOrderRepository.save(
                        paymentOrder
                );

        for (Order order : savedPaymentOrder.getOrders()) {

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

    private Map<String,String> buildVNPayParams(
            User user,
            Long amount,
            Long paymentOrderId
    ){

        Map<String,String> params =
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
                VNPayUtil.generateTxnRef(
                        paymentOrderId
                )
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