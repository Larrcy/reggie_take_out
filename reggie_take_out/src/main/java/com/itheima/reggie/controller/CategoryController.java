package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
//分类管理
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    //新增分类
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Category category) {
        log.info("category:{}", category);

        categoryService.save(category);

        return R.success("新增分类成功");
    }

    @GetMapping("/page")
    //分页查询
    public R<Page> page(int page, int pageSize) {
        log.info("page = {},pageSize ={},name={}", page, pageSize);
        //构造分页构造器
        Page<Category> pageInfo = new Page(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询（全局查询即可 前端未写条件查询）
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);

    }

    //删除分类
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类，id为：{}",id);
        //categoryService.removeById(id);
        categoryService.remove(id);

        return R.success("分类信息删除成功");
    }
    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    //post请求加@RequestBody
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);

        categoryService.updateById(category);

        return R.success("修改分类信息成功");
    }
    @GetMapping("/list")
    //get请求不加@RequestBody
    public R<List<Category>> list( Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper =new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list =categoryService.list(queryWrapper);
        return R.success(list);
    }
}

