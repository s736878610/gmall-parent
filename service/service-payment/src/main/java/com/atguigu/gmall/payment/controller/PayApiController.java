package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("api/payment")
public class PayApiController {

    @Autowired
    PaymentService paymentService;

    /**
     * 生成扫码支付页面
     * @param orderId
     * @return
     * @throws Exception
     */
    @RequestMapping("alipay/submit/{orderId}")
    public String alipay(@PathVariable Long orderId) throws Exception{
        String form = paymentService.alipayTradePagePay(orderId);
        System.out.println(form);
        return form;
    }

    @RequestMapping("alipay/callback/return")
    public String callback(HttpServletRequest request){
        // 接收支付宝回调信息
        String tradeNo = request.getParameter("trade_no");// 交易码
        String outTradeNo = request.getParameter("out_trade_no");// 外部交易码
        String callbackContent = request.getQueryString();// 回调信息

        // 封装数据
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setTradeNo(tradeNo);
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setCallbackTime(new Date());// 回调时间
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());
        paymentInfo.setCallbackContent(callbackContent);
        // 修改支付信息
        paymentService.updatePayment(paymentInfo);

        // 返回支付成功页面
        return "<form  action=\"http://payment.gmall.com/payment/success.html\">\n" +
                "</form>\n" +
                "<script>document.forms[0].submit();</script>";
    }

    /**
     * 测试 查询交易状态
     * @param outTradeNo
     * @return
     */
    @RequestMapping("alipay/query")
    public Result alipayQuery(String outTradeNo){
        Map<String,Object> map = paymentService.alipayQuery(outTradeNo);
        return Result.ok(map);
    }

}
