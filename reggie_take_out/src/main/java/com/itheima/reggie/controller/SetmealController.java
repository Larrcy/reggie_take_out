package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    //新增套餐

    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清理setmealCache下的所有缓存
    @ApiOperation(value = "新增套餐接口")
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }
    //分页查询
    @GetMapping("/page")
    @ApiOperation(value = "套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = false)
    })
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page(page, pageSize);
        Page<SetmealDto> dtoPage = new Page();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //添加排序条件 根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo, queryWrapper);
        //setmeal里没有分类这一项
        //需要将setmeal里的对象拷贝到SetmealDto中
        //Page中的records泛型与dtoPage的泛型不同 要进行手动添加
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            //将分类名称添加到setmealDto
            SetmealDto setmealDto = new SetmealDto();
            //将Setmeal的对象拷贝到SetmealDto
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类的名称
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);

    }
    //删除套餐
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清理setmealCache下的所有缓存
    @ApiOperation(value = "套餐删除接口")
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除分类，ids为：{}",ids);
        //categoryService.removeById(id);
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }
    //回显数据
    @GetMapping("/{id}")
    //post请求加@RequestBody
    public R<SetmealDto> get(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithSetmeal(id);

        return R.success(setmealDto);
    }

    //修改套餐
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setmealService.updateWithSetmeal(setmealDto);
        return R.success("修改套餐成功");
    }

    //对套餐进停售操作
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        Setmeal setmeal = new Setmeal();
        for (Long dishId : ids) {
            setmeal.setId(dishId);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }
    //移动端查询套餐信息
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    @ApiOperation(value = "套餐条件查询接口")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

}
