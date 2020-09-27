package com.knctrobocon.bluetoothconnector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import com.unity3d.player.UnityPlayer;

public class BluetoothController {
    private BluetoothAdapter _bluetoothAdapter;
    private int isConnected = -1;

    OutputStream _outputStream;
    InputStream _inputStream;
    BluetoothDevice _selectDevice;

    public void BluetoothInit() {
        UnityPlayer.UnitySendMessage("BluetoothControl","ReceiveData","Initializing");

        if(!IsBluetoothAvailable()){
            UnityPlayer.UnitySendMessage("BluetoothControl","ErrorMessage","Bluetooth_Not_Available");
        }
        else{
            UnityPlayer.UnitySendMessage("BluetoothControl","SearchDevices",SearchDevices());
        }
    }

    public void BluetoothService(){
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean IsBluetoothAvailable(){
        if(_bluetoothAdapter==null){
            return false;
        }
        else{
            return true;
        }
    }

    Set<BluetoothDevice>pairedDevices;
    public String SearchDevices(){
        String connectInfo="";

        if(_bluetoothAdapter.isEnabled()){
            _bluetoothAdapter.startDiscovery();

            pairedDevices = _bluetoothAdapter.getBondedDevices();

            if(pairedDevices.size()>0){
                for(BluetoothDevice device:pairedDevices){
                    connectInfo += device.getName().toString()+"\n";
                    connectInfo += device.getAddress().toString()+"\n";
                }
            }
        }
        else{
            connectInfo +="No connection";
        }
        return connectInfo;
    }

    public void ConnectDevice(String deviceName){
        if(pairedDevices.size()>0){
            UnityPlayer.UnitySendMessage("BluetoothControl","ReceiveData","Connecting...");

            for(BluetoothDevice device:pairedDevices){
                if(device.getName().equals(deviceName)){
                    _selectDevice=device;
                }
            }
        }

        UUID _uuid=UUID.fromString("e40c91de-77f4-49ad-bc4c-b4de51560caf");

        try{
            BluetoothSocket mSocket = _selectDevice.createRfcommSocketToServiceRecord(_uuid);
            mSocket.connect();
        }
        catch (IOException e){
            UnityPlayer.UnitySendMessage("BluetoothControl","ErrorMessage","Connection_Error");
        }
    }

    Thread _thread;
    boolean stopThread=false;

    private void ReceiveData(){
        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()&&!stopThread){
                    try{
                        int receiveBytes=_inputStream.available();
                        if(receiveBytes>0){
                            byte[] packetBytes=new byte[receiveBytes];
                            _inputStream.read(packetBytes);
                            final String data = new String(packetBytes,0,receiveBytes);
                            UnityPlayer.UnitySendMessage("BluretoothControl","ReceiveData",data);
                        }
                    }
                    catch(IOException e){
                        stopThread=true;
                    }
                }
            }
        });
        _thread.start();
    }

    public void StopThread(){
        stopThread=true;
    }

    public int IsConnected(){
        return isConnected;
    }
}
