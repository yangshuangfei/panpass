package com.panpass.gateway.biz.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2020-06-14
 */
@Data
@Accessors(chain = true)
public class GateWayConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String serviceId;

    private String uri;

    private String predicates;

    private String filters;

    private String orderNo;

    private String creatorId;

    private Date createDate;

    private String updateId;

    private Date updateTime;

    private String remarks;

    private Integer isDelete;


}