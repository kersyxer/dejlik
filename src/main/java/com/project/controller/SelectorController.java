package com.project.controller;

import com.project.dto.SelectorUserDto;
import com.project.entity.Partner;
import com.project.entity.Source;
import com.project.service.PartnerRepository;
import com.project.service.SourceRepository;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/Selectors")
@RequiredArgsConstructor
public class SelectorController {
    private final UserService userService;
    private final SourceRepository sourceRepository;
    private final PartnerRepository partnerRepository;

    @GetMapping("/Users")
    public List<SelectorUserDto> getUsers(){
        return userService.getAllUsers().stream()
                .map(user -> new SelectorUserDto(user.getId(), user.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/TrafficSources")
    public List<String> getTrafficSources(){
        return sourceRepository.findAll().stream()
                .map(Source::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    @GetMapping("/AffiliateNetworks")
    public List<String> getAffiliateNetworks(){
        return partnerRepository.findAll().stream()
                .map(Partner::getName)
                .distinct()
                .collect(Collectors.toList());
    }
}
