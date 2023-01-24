package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.mapper.DishMapper;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品 同时插入菜品对应的口味数据
    //同时向dish 和dishflavor 两张表里插入数据
    public  void  saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);

    //修改菜品
    //更新菜品信息同时更新对应的口味信息
    public  void updateWithFlavor(DishDto dishDto);

  public   void deleteByIds(List<Long> ids);
}
