package com.utnGymGroup.gym_system.features.fulllRoutine;

import com.utnGymGroup.gym_system.features.fulllRoutine.exception.FullRoutineNotFound;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FullRoutineService {

    private final FullRoutineRepository fullRoutineRepository;
    private final FullRoutineMapper fullRoutineMapper;


    private FullRoutineDtoResponse findById(UUID id) {
        FullRoutineEntity fullRoutineDtoResponse = fullRoutineRepository.findById(id)
                .orElseThrow(() -> new FullRoutineNotFound("No se encontro la rutina con id " + id));


        return fullRoutineDtoResponse;
    }

}
