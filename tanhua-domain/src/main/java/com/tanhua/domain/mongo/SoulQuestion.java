package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "soul_question")
public class SoulQuestion implements Serializable {
    @Id
    private ObjectId id; //主键id

    @Indexed
    private Integer questionnaireId;

    @Indexed
    private Long questionId; // 问题编号 优化:可用主键id代替 todo
    private String questionName;


}


