package cn.cyrus.otm.controller;

import cn.cyrus.otm.service.CommitService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@Api(tags = "校园卡业务大屏")
@RestController
@RequestMapping("/commit")
public class CommitSqlController {


    @Resource
    private CommitService commitService;


    @ApiOperationSupport(order = 1)
    @ApiOperation("提交")
    @GetMapping( "/c")
    public String top(String str){
        return commitService.changeSql(str);
    }


}
