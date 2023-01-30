package com.itheima.reggie.controller;

import com.alibaba.druid.sql.visitor.functions.Char;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Scanner;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @RequestMapping("/login")
    //json形式的数据要加@RequestBody
    //登录成功后要将员工对象的id传入session故要设置HttpServletRequest
    //通过request对象获取session
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        String password = employee.getPassword();
        //1.利用工具类的DigestUtils对密码进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名username查询数据库
        //包装查询对象
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //进行等值查询
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        //用户名已设置唯一约束故用getone方法
        Employee emp = employeeService.getOne(queryWrapper);
        //3.若没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("用户名未注册");
        }
        //4.若查询到则进行密码比对 若密码不一致则登录失败
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误,登录失败");
        }
        //5.查看员工状态是否禁用 若禁用则登录失败
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        //6.登录成功将员工id存入session并返回登录成功
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    //退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //新增员工
    //不添加@RequestBody 前端页面会报500
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息{}", employee.toString());
        //对应emplouyee对象进行设置
        //设置初始密码 并用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增成功");

    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize ={},name={}", page, pageSize, name);
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);

    }

    /*
        根据id修改员工信息
         */
    @PutMapping

    //post请求可加requestbody json数据模式
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        //只有管理员才有资格对员工账号进行启用禁用 故要利用request获取管理员id
        log.info(employee.toString());
        long id = Thread.currentThread().getId();
        log.info("线程id为：{}", id);

        // Long empId = (Long)request.getSession().getAttribute("employee");
        // employee.setUpdateTime(LocalDateTime.now());
        // employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息更新成功");

    }

    @GetMapping("/{id}")
    //接收请求路径中占位符的值
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应的员工");
    }

}

