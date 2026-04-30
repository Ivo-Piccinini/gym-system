package com.utnGymGroup.gym_system.common.interfaces;

public interface IMapper <Entity, Dto>{
    Dto convertToDto(Entity entity);
    Entity convertToEntity(Dto dto);
}
