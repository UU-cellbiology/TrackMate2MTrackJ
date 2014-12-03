%% function imports MTrackJ mdf file into cell tracks
function [tracks] = importMTrackJTracks(filenamein)

    tracks = {};
    nTotalTracks = 0;
    %open file for reading
    fid = fopen(filenamein,'r');
    %checking whether it is a MTrackJ file
    tline = fgetl(fid);
    if strcmp(tline(1:7),'MTrackJ')               
        tline = fgetl(fid);
        while ischar(tline)    
            %ignoring all the other fields apart from tracks and
            %points information
            %new track
            if strcmp(tline(1:5),'Track')
                nTotalTracks = nTotalTracks + 1; 
                nPointN = 1;
                tracks{nTotalTracks,1} = [];              
            end
            %new point
            if strcmp(tline(1:5),'Point')                        
                parsed = sscanf(tline(7:length(tline)),'%d %f %f %f %f %f');
                tracks{nTotalTracks,1}(nPointN,:) = parsed(2:6)'; 
                nPointN = nPointN +1;                        
            end
                tline = fgetl(fid);                    
        end                
    else
        disp('Error in MTrackJ file header!');
    end

end