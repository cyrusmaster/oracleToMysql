package cn.cyrus.otm.controller;

import cn.cyrus.otm.service.CommitService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@Api(tags = "校园卡业务大屏")
@RestController
@RequestMapping("/commit")
public class CommitSqlController {


    @Resource
    private CommitService commitService;


     /**
      * REMARK     类型   type   默认 0不格式化   1格式化
      * @className   CommitSqlController
      * @date  2023/2/2 14:21
      * @author  cyf
      */
    @ApiOperationSupport(order = 1)
    @ApiOperation("提交")
    @GetMapping( "/c")
    public String top(@RequestParam(value = "sql" ,required = true) String str ,@RequestParam(value = "type" ,required = false) Integer type){
        return commitService.changeSql(str , type);
    }


}
