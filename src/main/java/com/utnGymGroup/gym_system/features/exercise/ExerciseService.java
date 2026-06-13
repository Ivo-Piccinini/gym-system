package com.utnGymGroup.gym_system.features.exercise;

import com.utnGymGroup.gym_system.common.auth.permissions.Roles;
import com.utnGymGroup.gym_system.features.exercise.exceptions.ExerciseAlreadyExistsException;
import com.utnGymGroup.gym_system.features.exercise.exceptions.ExerciseNotFoundException;
import com.utnGymGroup.gym_system.features.exercise.exceptions.RoleNotValid;
import com.utnGymGroup.gym_system.features.user.UserService;
import com.utnGymGroup.gym_system.features.user.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ExerciseService
{
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;
    private final UserService userService;


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

    public ExerciseDto deleteExercise(UUID publicID, String userEmail)
    {
        UserDTO user = userService.findByEmail(userEmail);

        if(user.getRole().equals(Roles.ROLE_CLIENT))
        {
            throw new RoleNotValid("El cliente no puede realizar este cambio");
        }

        ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publicID)
                .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercico con ese nombre"));

       return exerciseMapper.convertToDto(exerciseRepository.save(exerciseEntity));

    }

   public ExerciseDto createExercise(ExerciseDto exerciseDto,String userEmail)
   {
       UserDTO user = userService.findByEmail(userEmail);

       if(user.getRole().equals(Roles.ROLE_CLIENT))
       {
           throw new RoleNotValid("El cliente no puede realizar este cambio");
       }

       if(exerciseRepository.existsByName(exerciseDto.getName()))
       {
           throw new ExerciseNotFoundException("Ya existe ese ejercicio");
       }
       ExerciseEntity exerciseEntity = exerciseMapper.convertToEntity(exerciseDto);
       return exerciseMapper.convertToDto(exerciseRepository.save(exerciseEntity));
   }

   public ExerciseDto updateExercise(ExerciseDto exerciseDto,UUID publidID,String userEmail)
   {
       UserDTO user = userService.findByEmail(userEmail);

       if(user.getRole().equals(Roles.ROLE_CLIENT))
       {
           throw new RoleNotValid("El cliente no puede realizar este cambio");
       }

      ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publidID)
              .orElseThrow(()-> new ExerciseNotFoundException("No se encontro ejercicio"));

        exerciseMapper.updateEntityFromDTO(exerciseDto,exerciseEntity);

        return exerciseMapper.convertToDto(exerciseRepository.save(exerciseEntity));
   }


   public ExerciseDto findByPublicId(UUID publidId)
   {
       ExerciseEntity exerciseEntity = exerciseRepository.findByIdPublic(publidId)
               .orElseThrow(()->new ExerciseNotFoundException("No se encontro el ejercicio"));

       return  exerciseMapper.convertToDto(exerciseEntity);

   }


}
