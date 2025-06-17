package com.sinloingok.app.models.activity;

import lombok.Data;

/**
 * 关于我们
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class ActivityIntro {
    /**
     *
     */
    private static final long serialVersionUID = 1116384023927844841L;

    private Long id;
    private String type;
    private String title;
    private String description;
    private String bank;

}
