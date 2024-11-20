package com.cpuoverload.intelliedu.model.dto.scoring;

import com.cpuoverload.intelliedu.model.entity.Application;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class DoScoreRequest implements Serializable {
    private static final long serialVersionUID = 6504624122615045534L;

    private List<String> AnswerList;

    private Application application;
}
