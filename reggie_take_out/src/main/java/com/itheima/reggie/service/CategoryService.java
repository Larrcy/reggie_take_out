package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    /*
        根据id删除分类 删除之前查询当前分类是否关联了其他菜品
         */
    public void remove(Long id);
}
