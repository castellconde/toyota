"# Agregar Addenda Toyota a CFDI" 

Se anida la adenda dentro del elemento cfdi:Comprobante

Referencia:  AddendaToyota xmlns="http://www.pegasotecnologia.com/secfd/schemas/AddendaReceptorToyota"

Se reemplaza la adenda en caso de que ya exista

Se toma el total del CFDI para el atributo Amount

Se concatena el atributo NoIdentificacion en el Shipment ID

AddToyota-1.0.jar [FilePath] [ShipmentId]


Recibe como parametros:
	
	args[0] : Ruta del archivo xml
	args[1] : Shipment ID




