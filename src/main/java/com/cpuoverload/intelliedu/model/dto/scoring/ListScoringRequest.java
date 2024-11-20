package com.cpuoverload.intelliedu.model.dto.scoring;

import com.cpuoverload.intelliedu.common.dto.TableRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListScoringRequest extends TableRequest implements Serializable {

    private static final long serialVersionUID = -8543001962339334880L;

    /**
     * Application ID
     */
    private Long appId;
}
