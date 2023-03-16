package com.dlut.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("AlphaDaoImp2")
@Primary
public class AlphaDaoImp2 implements AlphaDao{
    @Override
    public void select() {
        System.out.println("Mybatis");
    }
}
