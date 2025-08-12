package com.banco.banca.domain.service.Interface;

import com.banco.banca.domain.entity.Client;

import java.util.List;
import java.util.UUID;

public interface IClientService {
       List<Client> findAll();

        List<Client> search(String identificationType,
                            String identificationNumber,
                            String firstName,
                            String lastName,
                            String email);

        Client get(UUID id);

        Client create(Client client);

        Client update(UUID id, Client changes);

        void delete(UUID id);


}
