//package com.project.ecommerce.Controller;
//
//import com.project.ecommerce.Model.Cart;
//import com.project.ecommerce.Model.Coupon;
//import com.project.ecommerce.Model.User;
//import com.project.ecommerce.Service.CartService;
//import com.project.ecommerce.Service.CouponService;
//import com.project.ecommerce.Service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/coupons")
//public class AdminCouponController {
//
//    private final CouponService couponService;
//    private final CartService cartService;
//    private final UserService userService;
//
//    @PostMapping("/apply")
//    public ResponseEntity<Cart> applyCoupon (
//            @RequestParam String apply,
//            @RequestParam String code,
//            @RequestParam double orderValue,
//            @RequestHeader("Authorization") String jwt
//            ) throws Exception {
//        User user = userService.findUserByJwtToken(jwt);
//        Cart cart;
//        if(apply.equals("true")){
//            cart = couponService.applyCoupon(code,orderValue,user);
//        }
//        else {
//            cart = couponService.removeCoupon(code,user);
//        }
//        return ResponseEntity.ok(cart);
//    }
//
//    @PostMapping("/admin/create")
//    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon){
//        Coupon createdCoupon = couponService.createCoupon(coupon);
//        return ResponseEntity.ok(createdCoupon);
//    }
//
//    @DeleteMapping("/admin/delete/{id}")
//    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) throws Exception {
//        couponService.deleteCoupon(id);
//        return ResponseEntity.ok("Coupon deleted successfully");
//    }
//
//    @GetMapping("/admin/all")
//    public ResponseEntity<List<Coupon>> getAllCoupons(){
//        List<Coupon> coupons = couponService.findAllCoupons();
//        return ResponseEntity.ok(coupons);
//    }
//}
