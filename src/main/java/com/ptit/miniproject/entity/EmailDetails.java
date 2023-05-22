package com.ptit.miniproject.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EmailDetails {

    private String recipient;
    private String msgBody;
    private String subject;
}
