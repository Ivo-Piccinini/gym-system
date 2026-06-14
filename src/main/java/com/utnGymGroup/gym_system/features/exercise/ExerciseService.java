package com.utnGymGroup.gym_system.features.exercise;

import com.utnGymGroup.gym_system.features.audit.AuditActions;
import com.utnGymGroup.gym_system.features.audit.Auditable;
import com.utnGymGroup.gym_system.features.exercise.exceptions.ExerciseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ExerciseService
{
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapperRequest exerciseMapperRequest;
    private final ExerciseMapperRespond exerciseMapperRespond;


    public List<ExerciseDtoResponse> getAllExercise()
    {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapperRespond:: convertToDto)
                .toList();
    }

    public ExerciseDtoResponse findByName(String nombEjercicio)
    {
        ExerciseDtoResponse exercisedto = exerciseMapperRespond.convertToDto(exerciseRepository.findByName(nombEjercicio)
                .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercicio con nombre " + nombEjercicio)));
        return exercisedto;
    }


    @Auditable(AuditActions.DELETE_EXERCISE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ExerciseDtoResponse deleteExercise(Long publicID)
    {
        ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publicID)
                .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercico con ese id"));


        exerciseEntity.setEnabled(false);
        return exerciseMapperRespond.convertToDto(exerciseRepository.save(exerciseEntity));

    }

    @Auditable(AuditActions.CREATE_EXERCISE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ExerciseDtoResponse createExercise(ExerciseDtoRequest exerciseDtoRequest)
    {
        if(exerciseRepository.existsByName(exerciseDtoRequest.getName()))
        {
            throw new ExerciseNotFoundException("Ya existe ese ejercicio");
        }

        ExerciseEntity exerciseEntity = exerciseMapperRequest.convertToEntity(exerciseDtoRequest);
        return exerciseMapperRespond.convertToDto(exerciseRepository.save(exerciseEntity));
    }

    @Auditable(AuditActions.UPDATE_EXERCISE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ExerciseDtoResponse updateExercise(ExerciseDtoRequest exerciseDtoRequest, Long publidID)
    {
        ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publidID)
                .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercicio"));

        exerciseMapperRequest.updateEntityFromDTO(exerciseDtoRequest,exerciseEntity);

        return exerciseMapperRespond.convertToDto(exerciseRepository.save(exerciseEntity));
    }


    public ExerciseDtoResponse findByPublicId(Long publidId)
    {
        ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publidId)
                .orElseThrow(()->new ExerciseNotFoundException("No se encontro el ejercicio"));

        return  exerciseMapperRespond.convertToDto(exerciseEntity);

    }


}