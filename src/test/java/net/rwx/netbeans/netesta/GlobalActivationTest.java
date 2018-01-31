/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class GlobalActivationTest {

    @Test
    public void should_BeActivatedByDefault() {
        assertThat(GlobalActivation.isActivated()).isTrue();
    }
    
}
