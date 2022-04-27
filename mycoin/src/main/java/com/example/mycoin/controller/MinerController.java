package com.example.mycoin.controller;

import com.example.mycoin.miner.Miner;
import com.example.mycoin.miner.MinerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/miner")
public class MinerController {

    private MinerManager minerManager = MinerManager.getInstance();

    @RequestMapping(method = GET)
    public Miner getMiner(@RequestParam("name") String name) {
        return minerManager.getMiner(name);
    }

    @RequestMapping(method = DELETE)
    public void deleteMiner(@RequestParam("name") String name) {
        minerManager.deleteMiner(name);
    }

    @PostMapping("/miners")
    public ResponseEntity<?> addMiner(@RequestParam("name") String name, @RequestParam("port") int port) {
        var miner = minerManager.addMiner(name, port);

        return ResponseEntity.ok("done");
    }

    @RequestMapping(path = "all", method = GET)
    public List<Miner> getAllMiners() {
        return minerManager.getAllMiner();
    }
}