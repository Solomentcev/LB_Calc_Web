package com.lb_calc_web.service;

import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.ALSLB;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.repository.ALSLBRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
@Service
public class ALSLBService {
    private final ALSLBRepository alslbRepository;
    public ALSLBService(ALSLBRepository alslbRepository) {
        this.alslbRepository = alslbRepository;
    }
    public Set<ALSLB> addLBtoALS(ALS als, LB lb) {
        Set<ALSLB> quantityLB=als.getQuantityLB();
        if(quantityLB.isEmpty()){
            quantityLB.add(new ALSLB(als,lb,1));
        }else{
            int i=0;
            for(ALSLB alslb:quantityLB) {
                if (alslb.getLb().equals(lb)) {
                    alslb.setQuantity(alslb.getQuantity() + 1);
                    break;
                }
                i++;
            }
            if(i==quantityLB.size()){
                quantityLB.add(new ALSLB(als,lb,1));
            }
        }
        alslbRepository.saveAll(quantityLB);
        return quantityLB;
    }
    public Set<ALSLB> deleteLBfromALS(ALS als, LB lb) {
        Set<ALSLB> quantityLB=als.getQuantityLB();
        for(ALSLB alslb:quantityLB){
            if (alslb.getLb().equals(lb) && alslb.getQuantity()>1){
                alslb.setQuantity(alslb.getQuantity()-1);
                break;
            } else {
                quantityLB.remove(alslb);
                break;
            }
        }
        alslbRepository.saveAll(quantityLB);
        return quantityLB;
    }

    public void saveAll(Set<ALSLB> quantityLB) {
        alslbRepository.saveAll(quantityLB);
    }
}
