package de.hiwiapp.dto;


import java.util.ArrayList;
import java.util.List;

public class StundenUpdateForm {

    private List<MonatsEintrag> eintraege = new ArrayList<>();

    public List<MonatsEintrag>  getEintraege() {

        return  eintraege;
    }
    public void setEintraege(List<MonatsEintrag>  eintraege) {

        this.eintraege = eintraege;


    }



}
