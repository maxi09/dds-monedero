package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private BigDecimal saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = new BigDecimal(0);
  }

  public Cuenta(BigDecimal montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public long cantidadMovimientos(){
    return getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count();
  }

  public BigDecimal limiteExtraccion(){
    BigDecimal limite = new BigDecimal(1000).subtract(getMontoExtraidoA(LocalDate.now()));
    return limite;
  }

  public void validarMontoNegativo(BigDecimal monto){
    if (monto.doubleValue() <= 0) {
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void validarCantidadDeMovimiento(){
    if (this.cantidadMovimientos() >= 3){
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  public void validarHaySaldo(BigDecimal montoAExtraer){
    if (getSaldo().subtract(montoAExtraer).intValue() < 0 ){
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  public void validarLimiteExtraccion(BigDecimal cuanto){
    if (cuanto.compareTo(limiteExtraccion()) > 0 ){
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limiteExtraccion());
    }
  }

  public void poner(BigDecimal cuanto) {
    validarMontoNegativo(cuanto);
    validarCantidadDeMovimiento();
    this.agregarMovimiento(new Movimiento(LocalDate.now(), cuanto, true));
  }

  public void sacar(BigDecimal cuanto) {
    validarMontoNegativo(cuanto);
    validarHaySaldo(cuanto);
    validarLimiteExtraccion(cuanto);
    agregarMovimiento(new Movimiento(LocalDate.now(), cuanto, false));
  }


  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public BigDecimal getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.fueExtraido(fecha))
        .map(movimiento-> movimiento.getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public BigDecimal getSaldo(){
    return saldo.add(getMovimientos().stream().map(movimiento->movimiento.getMonto()).reduce(BigDecimal.ZERO,BigDecimal::add));
  }

  public void setSaldo(BigDecimal saldo) {
    this.saldo = saldo;
  }

}
