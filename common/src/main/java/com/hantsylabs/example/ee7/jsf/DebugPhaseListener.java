/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hantsylabs.example.ee7.jsf;

import java.util.logging.Logger;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;

/**
 *
 * @author hantsy
 */
public class DebugPhaseListener implements PhaseListener{
    
    @Inject Logger log;

    @Override
    public void afterPhase(PhaseEvent event) {
       log.info(" afer phase @"+ event.getPhaseId());
    }

    @Override
    public void beforePhase(PhaseEvent event) {
       log.info( " before phase @"+ event.getPhaseId());
    }

    @Override
    public PhaseId getPhaseId() {
       return PhaseId.ANY_PHASE;
    }
    
}
