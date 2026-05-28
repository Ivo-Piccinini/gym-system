package com.utnGymGroup.gym_system.features.exercise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ExerciseService
{
    public ExerciseRepository exerciseRepository;
    public ExerciseMapper exerciseMapper;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMapper = exerciseMapper;
    }

    public List<ExerciseDto> getAllExercise()
    {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapper :: convertToDto)
                .toList();
    }

    public ExerciseDto findByName(String nombEjercicio)
    {
        ExerciseDto exercisedto = exerciseMapper.convertToDto(exerciseRepository.findByName(nombEjercicio)
                .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercicio con nombre " + nombEjercicio)));
        return exercisedto;
    }







}
