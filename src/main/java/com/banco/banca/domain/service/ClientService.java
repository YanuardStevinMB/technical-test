package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.Client;
import com.banco.banca.domain.repository.ClientRepository;
import com.banco.banca.domain.service.Interface.IClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ClientService implements IClientService {

    private final ClientRepository clientRepository;

    // Stricter email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public List<Client> search(String identificationType,
                               String identificationNumber,
                               String firstName,
                               String lastName,
                               String email) {
        return clientRepository.search(
                blankToNull(identificationType),
                blankToNull(identificationNumber),
                blankToNull(firstName),
                blankToNull(lastName),
                blankToNull(email)
        );
    }

    public Client get(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }

    @Transactional
    public Client create(Client client) {
        validateClient(client);

        if (clientRepository.existsByIdentificationTypeAndIdentificationNumber(
                client.getIdentificationType(), client.getIdentificationNumber())) {
            throw new BusinessException(
                    "A client with the same identification type and number already exists",
                    HttpStatus.CONFLICT
            );
        }

        return clientRepository.save(client);
    }

    @Transactional
    public Client update(UUID id, Client changes) {
        Client current = get(id);

        // Age validation on update (if birth date is provided)
        if (changes.getBirthDate() != null) {
            int age = Period.between(changes.getBirthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new BusinessException("Client cannot be updated to a minor", HttpStatus.BAD_REQUEST);
            }
        }

        // Composite uniqueness only if identification changed
        boolean identificationChanged =
                (changes.getIdentificationType() != null && !changes.getIdentificationType().equals(current.getIdentificationType())) ||
                        (changes.getIdentificationNumber() != null && !changes.getIdentificationNumber().equals(current.getIdentificationNumber()));

        if (identificationChanged) {
            if (clientRepository.existsByIdentificationTypeAndIdentificationNumber(
                    changes.getIdentificationType(), changes.getIdentificationNumber())) {
                throw new BusinessException(
                        "A client with the same identification type and number already exists",
                        HttpStatus.CONFLICT
                );
            }
        }

        validateClient(changes);

        current.setIdentificationType(changes.getIdentificationType());
        current.setIdentificationNumber(changes.getIdentificationNumber());
        current.setFirstName(changes.getFirstName());
        current.setLastName(changes.getLastName());
        current.setEmail(changes.getEmail());
        current.setBirthDate(changes.getBirthDate());

        return clientRepository.save(current);
    }

    @Transactional
    public void delete(UUID id) {
        Client client = get(id);

        if (clientRepository.hasAssociatedAccounts(id)) {
            throw new BusinessException("Client with associated accounts cannot be deleted", HttpStatus.CONFLICT);
        }

        clientRepository.delete(client);
    }

    private void validateClient(Client client) {
        if (client.getBirthDate() == null) {
            throw new BusinessException("birthDate is required", HttpStatus.BAD_REQUEST);
        }

        int age = Period.between(client.getBirthDate(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new BusinessException("Client must be 18 years or older", HttpStatus.BAD_REQUEST);
        }

        if (client.getFirstName() == null || client.getFirstName().trim().length() < 2) {
            throw new BusinessException("firstName must have at least 2 characters", HttpStatus.BAD_REQUEST);
        }

        if (client.getLastName() == null || client.getLastName().trim().length() < 2) {
            throw new BusinessException("lastName must have at least 2 characters", HttpStatus.BAD_REQUEST);
        }

        if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(client.getEmail()).matches()) {
                throw new BusinessException("Invalid email format", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }
}
