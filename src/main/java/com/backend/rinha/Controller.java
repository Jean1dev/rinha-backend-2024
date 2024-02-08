package com.backend.rinha;

import com.backend.rinha.dto.ExtratoOutputDto;
import com.backend.rinha.dto.TransacaoInputDto;
import com.backend.rinha.dto.TransacaoOutputDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "clientes")
public class Controller {

    private final ServiceApplication serviceApplication;

    public Controller(ServiceApplication serviceApplication) {
        this.serviceApplication = serviceApplication;
    }

    @PostMapping("{id}/transacoes")
    public TransacaoOutputDto transacao(
            @RequestBody TransacaoInputDto transacaoInputDto, @PathVariable("id") Long id) {
        return serviceApplication.makeTransacao(id, transacaoInputDto);
    }

    @GetMapping("{id}/extrato")
    public ExtratoOutputDto extrato(@PathVariable("id") Long id) {
        return serviceApplication.extrato(id);
    }
}
