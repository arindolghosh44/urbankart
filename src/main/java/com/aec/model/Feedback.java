package com.aec.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int f_id;

    
   

    @Column(length = 500)
    private String fullName;

    @Column(length = 500)
    private String description;

    private int rating;

	private String email;
}
