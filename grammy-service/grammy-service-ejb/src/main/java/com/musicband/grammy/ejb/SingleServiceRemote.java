package com.musicband.grammy.ejb;

import com.musicband.grammy.model.AddSingleResponse;
import com.musicband.grammy.model.Single;
import jakarta.ejb.Remote;

@Remote
public interface SingleServiceRemote {
    
    AddSingleResponse addSingleToBand(Integer bandId, Single single);
}
