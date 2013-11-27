/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hantsylabs.example.ee7.jpa;

import java.util.Arrays;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hantsy
 */
@Converter
public class ListToStringConveter implements AttributeConverter<List, String> {
    
    @Override
    public String convertToDatabaseColumn(List attribute) {
       return StringUtils.join(attribute, ",");
    }

    @Override
    public List convertToEntityAttribute(String dbData) {
       String[] data=dbData.split(",");
       return Arrays.asList(data);
    }
}