package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    // 新增套餐，同时需要保存套餐和菜品的关联关系

    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    //删除套餐
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //停售套餐才能删除
        //查询套餐信息
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        //查询售卖中的套餐数据

        int count = this.count(queryWrapper);
        //批量删除操作要保证都停售才可以删除
        if (count > 0) {
            //不能删除则直接抛出异常
            throw new CustomException("套餐正在售卖 不可删除");
        }
        //若可以删除则先删除套餐表中的数据
        this.removeByIds(ids);
        // 然后再删除关系表中的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lambdaQueryWrapper);

    }


    //回显套餐
    @Override
    public SetmealDto getByIdWithSetmeal(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(id != null, SetmealDish::getSetmealId, id);
        if (setmeal != null) {
            //setmealDto里还有SetmealDishes需要手动添加
            BeanUtils.copyProperties(setmeal, setmealDto);
            List<SetmealDish> list = setmealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(list);
            return setmealDto;
        }
        return null;
    }

    @Override
    @Transactional
    public void updateWithSetmeal(SetmealDto setmealDto) {
        //更新套餐基本信息
        this.updateById(setmealDto);

        //清理当前套餐
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //创建集合新增套餐
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

}
