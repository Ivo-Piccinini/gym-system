package com.utnGymGroup.gym_system.features.exercise;

import com.utnGymGroup.gym_system.features.exercise.exceptions.ExerciseAlreadyExistsException;
import com.utnGymGroup.gym_system.features.exercise.exceptions.ExerciseNotFoundException;
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

    public ExerciseDto deleteExercise(String publicID)
    {
        ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publicID)
                .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercico con ese nombre"));

       return exerciseMapper.convertToDto(exerciseRepository.save(exerciseEntity));

    }

   public ExerciseDto createExercise(ExerciseDto exerciseDto)
   {
       if(exerciseRepository.existsByName(exerciseDto.getName()))
       {
           throw new ExerciseNotFoundException("Ya existe ese ejercicio");
       }
       ExerciseEntity exerciseEntity = exerciseMapper.convertToEntity(exerciseDto);
       return exerciseMapper.convertToDto(exerciseRepository.save(exerciseEntity));
   }

   public ExerciseDto updateExercise(ExerciseDto exerciseDto,String publidID)
   {
      ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publidID)
              .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercicio"));

        exerciseMapper.updateEntityFromDTO(exerciseDto,exerciseEntity);

        return exerciseMapper.convertToDto(exerciseRepository.save(exerciseEntity));
   }


   public ExerciseDto findByPublicId(String publidId)
   {
       ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publidId)
               .orElseThrow(()->new ExerciseNotFoundException("No se encontro el ejercicio"));

       return  exerciseMapper.convertToDto(exerciseEntity);

   }


}
