package com.example.mycoin.controller;

import com.example.mycoin.dto.BlockResponse;
import com.example.mycoin.dto.MinerResponse;
import com.example.mycoin.dto.TransactionResponse;
import com.example.mycoin.dto.WalletResponse;
import com.example.mycoin.entity.StringUtil;
import com.example.mycoin.miner.Miner;
import com.example.mycoin.miner.MinerManager;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/miner")
public class MinerController {
    private final ModelMapper modelMapper;
    private final MinerManager minerManager;

    @PostMapping("/miners")
    public ResponseEntity<?> addMiner(@RequestParam("port") int port) {
        var miner = minerManager.addMiner(port);

        return ResponseEntity.ok(modelMapper.map(miner, MinerResponse.class));
    }

    @GetMapping("/get-latest-transactions")
    public ResponseEntity<?> getLatestTransactions() {
        var latestItems = minerManager.getTransactions()
                .stream()
                .limit(100)
                .collect(Collectors.toList());

        if(latestItems == null || latestItems.size() == 0)
            return ResponseEntity.noContent().build();

        var resultData = latestItems.stream().map(item -> {
            var model = new TransactionResponse();

            model.setSender(StringUtil.getStringFromKey(item.sender));
            model.setReceiver(StringUtil.getStringFromKey(item.recipient));
            model.setValue(item.value);
            model.setId(item.transactionId);
            model.setTimeStamp(item.timeStamp);

            return model;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultData);

    }

    @GetMapping("/get-latest-blocks")
    public ResponseEntity<?> getLatestBlocks() {
        var latestItems = minerManager.getBlockchain()
                .stream()
                .limit(100)
                .collect(Collectors.toList());

        if(latestItems == null || latestItems.size() == 0)
            return ResponseEntity.noContent().build();

        var resultData = latestItems.stream().map(item -> {
            var model = new BlockResponse();

            model.setIndex(item.getIndex());
            model.setTimeStamp(item.getTimeStamp());
            model.setMiner(item.getMiner() != null ? item.getMiner().getName() : "root");
            model.setTransactionCount(item.getTransactions() != null ? item.getTransactions().size() : 0);
            model.setValue(item.getTransactionsValue());

            return model;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultData);
    }





//    @RequestMapping(method = GET)
//    public Miner getMiner(@RequestParam("name") String name) {
//        return minerManager.getMiner(name);
//    }
//
//    @RequestMapping(method = DELETE)
//    public void deleteMiner(@RequestParam("name") String name) {
//        minerManager.deleteMiner(name);
//    }
//
//    @RequestMapping(path = "all", method = GET)
//    public List<Miner> getAllMiners() {
//        return minerManager.getAllMiner();
//    }
//


}