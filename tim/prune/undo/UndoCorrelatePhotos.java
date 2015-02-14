package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an auto-correlation of photos with points
 */
public class UndoCorrelatePhotos implements UndoOperation
{
	private DataPoint[] _contents = null;
	private DataPoint[] _photoPoints = null;
	private int _numPhotosCorrelated = -1;


	/**
	 * Constructor
	 * @param inTrackInfo track information
	 */
	public UndoCorrelatePhotos(TrackInfo inTrackInfo)
	{
		// Copy track contents
		_contents = inTrackInfo.getTrack().cloneContents();
		// Copy points associated with photos before correlation
		int numPhotos = inTrackInfo.getPhotoList().getNumPhotos();
		_photoPoints = new DataPoint[numPhotos];
		for (int i=0; i<numPhotos; i++) {
			_photoPoints[i] = inTrackInfo.getPhotoList().getPhoto(i).getDataPoint();
		}
	}

	/**
	 * @param inNumCorrelated number of photos correlated
	 */
	public void setNumPhotosCorrelated(int inNumCorrelated)
	{
		_numPhotosCorrelated = inNumCorrelated;
	}

	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.correlate") + " (" + _numPhotosCorrelated + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().replaceContents(_contents);
		// restore photo association
		for (int i=0; i<_photoPoints.length; i++)
		{
			Photo photo = inTrackInfo.getPhotoList().getPhoto(i);
			DataPoint point = _photoPoints[i];
			photo.setDataPoint(point);
			if (point != null) {
				point.setPhoto(photo);
			}
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}