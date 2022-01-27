package com.tanhua.server.controller;


import com.tanhua.server.service.TanhuaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tanhua")
public class TanhuaController {

    @Autowired
    private TanhuaService tanhuaService;


    /**
     * 探花-不喜欢
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{id}/unlove",method = RequestMethod.GET)
    public ResponseEntity unlove(@PathVariable("id") Long userId){
        tanhuaService.unlove(userId);
        return ResponseEntity.ok(null);
    }

    /**
     * 探花喜欢
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{id}/love",method = RequestMethod.GET)
    public ResponseEntity love(@PathVariable("id") Long userId){
        tanhuaService.love(userId);
        return ResponseEntity.ok(null);
    }

}
