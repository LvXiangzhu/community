package com.dlut.community.dao;

import org.springframework.stereotype.Repository;

@Repository
public class AlphaDaoImp1 implements AlphaDao{
    @Override
    public void select() {
        System.out.println("AlphaDao");
    }
}
