package org.rif.notifier.models.entities;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Test{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private String id;


    private String testcol;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestcol() {
        return testcol;
    }

    public void setTestcol(String testcol) {
        this.testcol = testcol;
    }




    public Test() {

    }



}