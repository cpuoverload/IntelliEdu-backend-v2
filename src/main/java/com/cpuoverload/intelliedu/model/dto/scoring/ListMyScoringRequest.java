package com.cpuoverload.intelliedu.model.dto.scoring;

import com.cpuoverload.intelliedu.common.dto.TableRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListMyScoringRequest extends TableRequest implements Serializable {
    private static final long serialVersionUID = -364684559763216033L;

    /**
     * Application ID
     */
    private Long appId;
}
